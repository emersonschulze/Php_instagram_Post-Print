-- phpMyAdmin SQL Dump
-- version 4.4.15.1
-- http://www.phpmyadmin.net
--
-- Host: mysql552.umbler.com
-- Generation Time: 10-Dez-2017 às 22:29
-- Versão do servidor: 5.6.36-log
-- PHP Version: 5.4.8

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `postprintapi`
--
DROP DATABASE `postprintapi`;
CREATE DATABASE IF NOT EXISTS `postprintapi` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci;
USE `postprintapi`;

-- --------------------------------------------------------

--
-- Estrutura da tabela `event`
--

DROP TABLE IF EXISTS `event`;
CREATE TABLE IF NOT EXISTS `event` (
  `id_event` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `dt_event` date NOT NULL,
  `have_screen` char(1) NOT NULL DEFAULT 'N',
  `have_print` char(1) NOT NULL DEFAULT 'S',
  `automatic` char(1) NOT NULL DEFAULT 'N',
  `hashtag` varchar(40) DEFAULT NULL,
  `id_print_template` int(11) NOT NULL,
  `logo_event` varchar(255) NOT NULL,
  `active` char(1) NOT NULL DEFAULT 'N',
  `qtde_fotos` int(11) DEFAULT '0'
) ENGINE=InnoDB AUTO_INCREMENT=63 DEFAULT CHARSET=latin1;

--
-- Extraindo dados da tabela `event`
--

INSERT INTO `event` (`id_event`, `name`, `dt_event`, `have_screen`, `have_print`, `automatic`, `hashtag`, `id_print_template`, `logo_event`, `active`, `qtde_fotos`) VALUES
(58, 'Emerson Teste', '2017-11-12', 'S', 'S', 'S', 'emersonc3', 3, 'E:\\Fotos\\1 imagens maneiras\\games\\66.jpg', 'N', 10),
(61, 'Emerson Schulze', '2017-10-10', 'S', 'S', 'N', 'emersonschulze', 2, 'D:\\Fotos\\1 imagens maneiras\\games\\7.jpg', 'N', 100);

-- --------------------------------------------------------

--
-- Estrutura da tabela `instagram`
--

DROP TABLE IF EXISTS `instagram`;
CREATE TABLE IF NOT EXISTS `instagram` (
  `id_instagram` int(11) NOT NULL,
  `username` varchar(40) NOT NULL,
  `imagem_perfil` varchar(250) NOT NULL,
  `insta_token` varchar(250) NOT NULL,
  `id_suaID` int(11) NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;

--
-- Extraindo dados da tabela `instagram`
--

INSERT INTO `instagram` (`id_instagram`, `username`, `imagem_perfil`, `insta_token`, `id_suaID`) VALUES
(4, 'emersonschulze', 'https://scontent.cdninstagram.com/t51.2885-19/s150x150/13126669_963315463755768_591818836_a.jpg', '270817430.6c61e68.80a6f0177a744248a581147fe03e08ce', 270817430);

-- --------------------------------------------------------

--
-- Estrutura da tabela `print_template`
--

DROP TABLE IF EXISTS `print_template`;
CREATE TABLE IF NOT EXISTS `print_template` (
  `id_print_template` int(11) NOT NULL,
  `name` varchar(40) NOT NULL,
  `specs` text NOT NULL COMMENT 'Guarda json com padrão de posicionamentos',
  `layout_image` varchar(255) NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;

--
-- Extraindo dados da tabela `print_template`
--

INSERT INTO `print_template` (`id_print_template`, `name`, `specs`, `layout_image`) VALUES
(1, 'Template 1', '{"papel_width":"598","papel_height":"803","foto_width":"538","foto_height":"538","foto_left":"30","foto_top":"150","usuario_top":"723","usuario_left":"30","usuario_font":"Arial","usuario_font_size":"16","usuario_font_style":"NORMAL","usuario_align":"NORMAL","empresa_width":"140","empresa_height":"55","empresa_left":"428","empresa_top":"45","evento_width":"0","evento_height":"60","evento_left":"30","evento_top":"50","evento_font":"Arial","evento_font_size":"30","evento_font_style":"NORMAL","evento_align":"NORMAL"}', 'http://postprint.com.br/post_print.png'),
(2, 'Template 2', '{"papel_width":"598","papel_height":"803","foto_width":"538","foto_height":"538","foto_left":"30","foto_top":"100","usuario_top":"45","usuario_left":"30","usuario_font":"Arial","usuario_font_size":"16","usuario_font_style":"NORMAL","usuario_align":"NORMAL","empresa_width":"140","empresa_height":"55","empresa_left":"428","empresa_top":"30","evento_width":"0","evento_height":"115","evento_left":"","evento_top":"655","evento_font":"Arial","evento_font_size":"30","evento_font_style":"NORMAL","evento_align":"CENTER"}', 'http://postprint.com.br/post_print.png'),
(3, 'Template 3', '{"papel_width":"598","papel_height":"803","foto_width":"538","foto_height":"538","foto_left":"30","foto_top":"100","usuario_top":"45","usuario_left":"30","usuario_font":"Arial","usuario_font_size":"16","usuario_font_style":"NORMAL","usuario_align":"NORMAL","empresa_width":"140","empresa_height":"55","empresa_left":"428","empresa_top":"675","evento_width":"0","evento_height":"60","evento_left":"30","evento_top":"675","evento_font":"Arial","evento_font_size":"30","evento_font_style":"NORMAL","evento_align":"NORMAL"}', 'http://postprint.com.br/post_print.png');

-- --------------------------------------------------------

--
-- Estrutura da tabela `user`
--

DROP TABLE IF EXISTS `user`;
CREATE TABLE IF NOT EXISTS `user` (
  `id_user` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `login` varchar(120) NOT NULL COMMENT 'Email',
  `password` varchar(100) NOT NULL,
  `dt_created` datetime NOT NULL,
  `serial` varchar(100) DEFAULT NULL,
  `name_machine` varchar(255) DEFAULT NULL,
  `os_machine` varchar(255) DEFAULT 'Windows 7',
  `active` char(1) NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;

--
-- Extraindo dados da tabela `user`
--

INSERT INTO `user` (`id_user`, `name`, `login`, `password`, `dt_created`, `serial`, `name_machine`, `os_machine`, `active`) VALUES
(1, 'Emerson', 'contato@postprint.com.br', 'PostPrint.10', '2017-08-01 11:54:35', 'BBXi6AYtUtk+cmQ3ms18HcdjXFYndELBVWZOVkRhX8AfoeztV82VZ0JX/ACopHKF', 'emerson', 'Windows 10', 'S');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `event`
--
ALTER TABLE `event`
  ADD PRIMARY KEY (`id_event`);

--
-- Indexes for table `instagram`
--
ALTER TABLE `instagram`
  ADD PRIMARY KEY (`id_instagram`);

--
-- Indexes for table `print_template`
--
ALTER TABLE `print_template`
  ADD PRIMARY KEY (`id_print_template`);

--
-- Indexes for table `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`id_user`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `event`
--
ALTER TABLE `event`
  MODIFY `id_event` int(11) NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=63;
--
-- AUTO_INCREMENT for table `instagram`
--
ALTER TABLE `instagram`
  MODIFY `id_instagram` int(11) NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=5;
--
-- AUTO_INCREMENT for table `print_template`
--
ALTER TABLE `print_template`
  MODIFY `id_print_template` int(11) NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=4;
--
-- AUTO_INCREMENT for table `user`
--
ALTER TABLE `user`
  MODIFY `id_user` int(11) NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=2;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
